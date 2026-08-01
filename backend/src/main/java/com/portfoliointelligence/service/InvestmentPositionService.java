package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.AssetClassSummaryResponse;
import com.portfoliointelligence.dto.ConsolidatedInvestmentResponse;
import com.portfoliointelligence.dto.InvestmentPositionResponse;
import com.portfoliointelligence.dto.PortfolioSummaryResponse;
import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentPosition;
import com.portfoliointelligence.entity.InvestmentStyle;
import com.portfoliointelligence.repository.InvestmentPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class InvestmentPositionService {

    private static final int PERCENTAGE_SCALE = 6;

    private final InvestmentPositionRepository positionRepository;
    private final AnalysisService analysisService;

    public InvestmentPositionService(
            InvestmentPositionRepository positionRepository,
            AnalysisService analysisService
    ) {
        this.positionRepository = positionRepository;
        this.analysisService = analysisService;
    }

    @Transactional(readOnly = true)
    public List<InvestmentPositionResponse> findPositions(
            UUID analysisId
    ) {
        analysisService.getEntity(analysisId);

        return positionRepository
                .findAllByAnalysisIdOrderByAssetNameAsc(
                        analysisId
                )
                .stream()
                .map(InvestmentPositionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse summarize(
            UUID analysisId
    ) {
        analysisService.getEntity(analysisId);

        List<InvestmentPosition> positions =
                positionRepository
                        .findAllByAnalysisIdOrderByAssetNameAsc(
                                analysisId
                        );

        Map<String, PositionAccumulator> clusters =
                new LinkedHashMap<>();

        for (InvestmentPosition position : positions) {
            String clusterKey = createClusterKey(position);

            clusters
                    .computeIfAbsent(
                            clusterKey,
                            ignored ->
                                    new PositionAccumulator(
                                            clusterKey,
                                            position
                                    )
                    )
                    .add(position);
        }

        BigDecimal totalMarketValue = positions
                .stream()
                .map(
                        InvestmentPosition::getMarketValueUsd
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        List<ConsolidatedInvestmentResponse> consolidated =
                clusters
                        .values()
                        .stream()
                        .map(accumulator ->
                                accumulator.toResponse(
                                        totalMarketValue
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                ConsolidatedInvestmentResponse
                                                        ::marketValueUsd
                                        )
                                        .reversed()
                        )
                        .toList();

        List<AssetClassSummaryResponse> byAssetClass =
                createAssetClassSummary(
                        consolidated,
                        totalMarketValue
                );

        return new PortfolioSummaryResponse(
                analysisId,
                positions.size(),
                consolidated.size(),
                totalMarketValue,
                byAssetClass,
                consolidated
        );
    }

    private List<AssetClassSummaryResponse>
    createAssetClassSummary(
            List<ConsolidatedInvestmentResponse> assets,
            BigDecimal totalMarketValue
    ) {
        Map<AssetClass, List<ConsolidatedInvestmentResponse>>
                groupedAssets = new LinkedHashMap<>();

        for (ConsolidatedInvestmentResponse asset : assets) {
            groupedAssets
                    .computeIfAbsent(
                            asset.assetClass(),
                            ignored -> new ArrayList<>()
                    )
                    .add(asset);
        }

        return groupedAssets
                .entrySet()
                .stream()
                .map(entry -> {
                    BigDecimal value = entry
                            .getValue()
                            .stream()
                            .map(
                                    ConsolidatedInvestmentResponse
                                            ::marketValueUsd
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

                    return new AssetClassSummaryResponse(
                            entry.getKey(),
                            entry.getValue().size(),
                            value,
                            percentage(
                                    value,
                                    totalMarketValue
                            )
                    );
                })
                .sorted(
                        Comparator
                                .comparing(
                                        AssetClassSummaryResponse
                                                ::marketValueUsd
                                )
                                .reversed()
                )
                .toList();
    }

    private String createClusterKey(
            InvestmentPosition position
    ) {
        if (StringUtils.hasText(
                position.getIdentifier()
        )) {
            return "IDENTIFIER:"
                    + normalize(
                            position.getIdentifier()
                    );
        }

        return "NAME:"
                + normalize(
                        position.getAssetName()
                );
    }

    private BigDecimal percentage(
            BigDecimal value,
            BigDecimal total
    ) {
        if (total == null
                || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
                    .setScale(PERCENTAGE_SCALE);
        }

        return value
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        total,
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private String normalize(String value) {
        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private final class PositionAccumulator {

        private final String clusterKey;
        private String assetName;
        private String identifier;
        private AssetClass assetClass;
        private InstrumentType instrumentType;
        private InvestmentStyle investmentStyle;
        private BigDecimal quantity;
        private BigDecimal marketValue;
        private LocalDate earliestInvestmentDate;
        private final EnumSet<FinancialInstitution> institutions;
        private int sourcePositions;

        private PositionAccumulator(
                String clusterKey,
                InvestmentPosition initialPosition
        ) {
            this.clusterKey = clusterKey;
            this.assetName = initialPosition.getAssetName();
            this.identifier = initialPosition.getIdentifier();
            this.assetClass = initialPosition.getAssetClass();
            this.instrumentType =
                    initialPosition.getInstrumentType();
            this.investmentStyle =
                    initialPosition.getInvestmentStyle();
            this.quantity = null;
            this.marketValue = BigDecimal.ZERO;
            this.earliestInvestmentDate = null;
            this.institutions =
                    EnumSet.noneOf(
                            FinancialInstitution.class
                    );
            this.sourcePositions = 0;
        }

        private void add(
                InvestmentPosition position
        ) {
            marketValue = marketValue.add(
                    position.getMarketValueUsd()
            );

            if (position.getQuantity() != null) {
                quantity = quantity == null
                        ? position.getQuantity()
                        : quantity.add(
                                position.getQuantity()
                        );
            }

            if (position.getInvestmentDate() != null) {
                if (earliestInvestmentDate == null
                        || position
                        .getInvestmentDate()
                        .isBefore(
                                earliestInvestmentDate
                        )) {
                    earliestInvestmentDate =
                            position.getInvestmentDate();
                }
            }

            institutions.add(
                    position.getInstitution()
            );

            if (assetClass == AssetClass.UNKNOWN
                    && position.getAssetClass()
                    != AssetClass.UNKNOWN) {
                assetClass = position.getAssetClass();
            }

            if (instrumentType
                    == InstrumentType.UNKNOWN
                    && position.getInstrumentType()
                    != InstrumentType.UNKNOWN) {
                instrumentType =
                        position.getInstrumentType();
            }

            if (investmentStyle
                    == InvestmentStyle.UNKNOWN
                    && position.getInvestmentStyle()
                    != InvestmentStyle.UNKNOWN) {
                investmentStyle =
                        position.getInvestmentStyle();
            }

            if (!StringUtils.hasText(identifier)
                    && StringUtils.hasText(
                    position.getIdentifier()
            )) {
                identifier = position.getIdentifier();
            }

            sourcePositions++;
        }

        private ConsolidatedInvestmentResponse toResponse(
                BigDecimal totalMarketValue
        ) {
            List<FinancialInstitution> institutionList =
                    institutions
                            .stream()
                            .sorted(
                                    Comparator.comparing(
                                            Enum::name
                                    )
                            )
                            .toList();

            return new ConsolidatedInvestmentResponse(
                    clusterKey,
                    assetName,
                    identifier,
                    assetClass,
                    instrumentType,
                    investmentStyle,
                    quantity,
                    marketValue,
                    percentage(
                            marketValue,
                            totalMarketValue
                    ),
                    earliestInvestmentDate,
                    institutionList,
                    sourcePositions
            );
        }
    }
}