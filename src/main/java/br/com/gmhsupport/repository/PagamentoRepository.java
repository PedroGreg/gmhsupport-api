package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByChamadoId(Long chamadoId);

    // Soma do que o técnico já recebeu: chamados resolvidos e pagos. Volta null se não houver nenhum
    @Query("SELECT SUM(p.valor) FROM Pagamento p " +
           "WHERE p.chamado.tecnico.id = :tecnicoId AND p.chamado.status.id = 4 " +
           "AND p.status = br.com.gmhsupport.model.StatusPagamento.pago")
    BigDecimal somarRecebidoPorTecnico(@Param("tecnicoId") Long tecnicoId);
}
