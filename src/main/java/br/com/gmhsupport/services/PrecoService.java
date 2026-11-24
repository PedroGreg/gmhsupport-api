package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.response.OrcamentoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Prioridade;
import br.com.gmhsupport.model.SubcategoriaProblema;
import br.com.gmhsupport.repository.SubcategoriaProblemaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Regra de preço do atendimento (uma só, usada no orçamento e na abertura do chamado):
//   serviço      = valor base da subcategoria x (1 + acréscimo da prioridade)
//   deslocamento = (km em linha reta - km grátis) x preço por km   (nunca negativo)
//   total        = serviço + deslocamento
// Categoria de valor fixo (ex.: Testes): total = valor base, sem acréscimos e sem limite de distância.
// Os números ficam no application.properties (gmh.km.* e gmh.origem.*).
@Service
public class PrecoService {

    private static final double RAIO_TERRA_KM = 6371.0;

    private final SubcategoriaProblemaRepository subcategoriaRepository;
    private final double origemLatitude;
    private final double origemLongitude;
    private final BigDecimal precoKm;
    private final BigDecimal kmGratis;
    private final BigDecimal kmMaximo;

    public PrecoService(SubcategoriaProblemaRepository subcategoriaRepository,
                        @Value("${gmh.origem.latitude}") double origemLatitude,
                        @Value("${gmh.origem.longitude}") double origemLongitude,
                        @Value("${gmh.km.preco}") BigDecimal precoKm,
                        @Value("${gmh.km.gratis}") BigDecimal kmGratis,
                        @Value("${gmh.km.maximo}") BigDecimal kmMaximo) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.origemLatitude = origemLatitude;
        this.origemLongitude = origemLongitude;
        this.precoKm = precoKm;
        this.kmGratis = kmGratis;
        this.kmMaximo = kmMaximo;
    }

    @Transactional(readOnly = true)
    public OrcamentoResponseDTO orcar(Long subcategoriaId, Prioridade prioridade, Double latitude, Double longitude) {
        SubcategoriaProblema sub = subcategoriaRepository.findById(subcategoriaId)
                .orElseThrow(() -> new CustomNotFoundException("Subcategoria não encontrada com o ID: " + subcategoriaId));
        if (!Boolean.TRUE.equals(sub.getAtivo())) {
            throw new IllegalStateException("Este tipo de problema não está mais disponível.");
        }
        return orcar(sub, prioridade, latitude, longitude);
    }

    public OrcamentoResponseDTO orcar(SubcategoriaProblema sub, Prioridade prioridade, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalStateException("Informe a localização do atendimento.");
        }
        Prioridade p = prioridade != null ? prioridade : Prioridade.media;
        boolean fixo = sub.getCategoria() != null && Boolean.TRUE.equals(sub.getCategoria().getValorFixo());
        BigDecimal distancia = BigDecimal.valueOf(distanciaKm(latitude, longitude)).setScale(2, RoundingMode.HALF_UP);

        if (fixo) {
            BigDecimal base = sub.getValorBase().setScale(2, RoundingMode.HALF_UP);
            return new OrcamentoResponseDTO(sub.getId(), sub.getNome(), base, 0, base, distancia,
                    kmGratis, BigDecimal.ZERO, precoKm, BigDecimal.ZERO.setScale(2), base, kmMaximo, true, true);
        }

        BigDecimal base = sub.getValorBase();
        BigDecimal servico = base.multiply(BigDecimal.ONE.add(p.getAcrescimo())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal kmCobrados = distancia.subtract(kmGratis).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deslocamento = kmCobrados.multiply(precoKm).setScale(2, RoundingMode.HALF_UP);
        int percentual = p.getAcrescimo().multiply(BigDecimal.valueOf(100)).intValue();

        return new OrcamentoResponseDTO(sub.getId(), sub.getNome(), base.setScale(2, RoundingMode.HALF_UP), percentual, servico,
                distancia, kmGratis, kmCobrados, precoKm, deslocamento, servico.add(deslocamento),
                kmMaximo, distancia.compareTo(kmMaximo) <= 0, false);
    }

    // Distância em linha reta (fórmula de Haversine) entre o SENAC Jurubatuba e o ponto informado
    public double distanciaKm(double latitude, double longitude) {
        double dLat = Math.toRadians(latitude - origemLatitude);
        double dLon = Math.toRadians(longitude - origemLongitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(origemLatitude)) * Math.cos(Math.toRadians(latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
