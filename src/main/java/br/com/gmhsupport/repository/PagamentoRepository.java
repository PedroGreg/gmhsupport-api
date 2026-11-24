package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Pagamento;
import br.com.gmhsupport.model.StatusRepasse;
import br.com.gmhsupport.model.TipoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByChamadoId(Long chamadoId);

    Optional<Pagamento> findByChamadoIdAndTipo(Long chamadoId, TipoPagamento tipo);

    // Soma dos repasses do técnico em uma situação (liberado = a receber, pago = recebido). Volta null se não houver
    @Query("SELECT SUM(p.valorRepasse) FROM Pagamento p " +
           "WHERE p.chamado.tecnico.id = :tecnicoId AND p.repasseStatus = :status")
    BigDecimal somarRepassePorTecnico(@Param("tecnicoId") Long tecnicoId, @Param("status") StatusRepasse status);

    // Lista do admin (Pagamentos/Repasses)
    @Query("SELECT p FROM Pagamento p JOIN FETCH p.chamado c JOIN FETCH c.tecnico t " +
           "WHERE p.repasseStatus = :status ORDER BY t.nome ASC, p.dataRepasseLiberado ASC")
    List<Pagamento> listarRepasses(@Param("status") StatusRepasse status);

    @Query("SELECT p FROM Pagamento p WHERE p.chamado.tecnico.id = :tecnicoId AND p.repasseStatus = :status")
    List<Pagamento> repassesDoTecnico(@Param("tecnicoId") Long tecnicoId, @Param("status") StatusRepasse status);

    @Query("SELECT SUM(p.valorRepasse) FROM Pagamento p WHERE p.repasseStatus = :status")
    BigDecimal somarRepasses(@Param("status") StatusRepasse status);

    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.repasseStatus = :status")
    long contarRepasses(@Param("status") StatusRepasse status);
}
