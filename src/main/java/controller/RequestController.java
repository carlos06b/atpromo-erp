package controller;

import dao.FinancePromoterDAO;
import dao.PromoterDAO;
import dao.RequestDAO;
import model.FinancePromoter;
import model.Promoter;
import model.PromoterPaymentData;
import model.Request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestController {

    private RequestDAO requestDAO = new RequestDAO();
    private PromoterDAO promoterDAO = new PromoterDAO();

    public void createRequest(int idUserRH, int idUserFin, int idPromoter,
                              String type, BigDecimal amount, String message) {

        if (promoterDAO.findById(idPromoter) == null) {
            System.out.println("Promotor não existe!");
            return;
        }

        if (!isValidType(type)) {
            System.out.println("Tipo de solicitação inválido.");
            return;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Valor inválido.");
            return;
        }

        if (message == null || message.isBlank()) {
            System.out.println("Mensagem não pode ficar vazia.");
            return;
        }

        Request request = new Request();

        request.setId_UserRH(idUserRH);
        request.setId_UserFin(idUserFin);
        request.setId_Promoter(idPromoter);
        request.setType(type.toUpperCase());
        request.setAmount(amount);
        request.setMessage(message);
        request.setStatus("PENDENTE");
        request.setDate(LocalDateTime.now());

        requestDAO.save(request);
    }

    public void listAll() {

        List<Request> list = requestDAO.findAll();

        if (list.isEmpty()) {
            System.out.println("Nenhuma solicitação encontrada.");
            return;
        }

        for (Request r : list) {
            printRequest(r);
        }
    }

    public void listAllWithPromoterName() {

        List<String> list = requestDAO.findAllWithPromoterName();

        if (list.isEmpty()) {
            System.out.println("Nenhuma solicitação encontrada.");
            return;
        }

        for (String line : list) {
            System.out.println(line);
        }
    }

    public List<String> getAllWithPromoterName() {
        return requestDAO.findAllWithPromoterName();
    }

    public void listPendingWithPromoterName() {

        List<String> list = requestDAO.findPendingWithPromoterName();

        if (list.isEmpty()) {
            System.out.println("Nenhuma solicitação pendente.");
            return;
        }

        for (String line : list) {
            System.out.println(line);
        }
    }

    public List<String> getPendingWithPromoterName() {
        return requestDAO.findPendingWithPromoterName();
    }

    public void listByStatus(String status) {

        List<Request> list = requestDAO.findByStatus(status);

        if (list.isEmpty()) {
            System.out.println("Nenhuma solicitação com status: " + status);
            return;
        }

        for (Request r : list) {
            printRequest(r);
        }
    }

    public void listByPeriod(LocalDateTime start, LocalDateTime end) {

        List<String> list = requestDAO.findByPeriodWithPromoterName(start, end);

        if (list.isEmpty()) {
            System.out.println("Nenhuma solicitação nesse período.");
            return;
        }

        for (String line : list) {
            System.out.println(line);
        }
    }

    public List<String> getByPeriodWithPromoterName(LocalDateTime start, LocalDateTime end) {
        return requestDAO.findByPeriodWithPromoterName(start, end);
    }

    public void approve(int id) {
        approve(id, 0);
    }

    public void approve(int id, int idUserFin) {

        List<Request> list = requestDAO.findAll();

        for (Request r : list) {

            if (r.getId() == id) {

                if (!r.getStatus().equalsIgnoreCase("PENDENTE")) {
                    System.out.println("Essa solicitação já foi analisada.");
                    return;
                }

                if (promoterDAO.findById(r.getId_Promoter()) == null) {
                    System.out.println("Erro: promotor não existe mais.");
                    return;
                }

                if (idUserFin > 0) {
                    requestDAO.updateFinanceUser(id, idUserFin);
                }

                requestDAO.updateStatus(id, "APROVADO");

                FinancePromoter finance = new FinancePromoter();

                finance.setIdPromoter(r.getId_Promoter());
                finance.setType(r.getType());
                finance.setAmount(r.getAmount());
                finance.setDate(LocalDate.now());
                finance.setStatus("PAGO");

                FinancePromoterDAO financeDAO = new FinancePromoterDAO();
                financeDAO.save(finance);

                System.out.println("Solicitação aprovada e lançada no financeiro.");
                return;
            }
        }

        System.out.println("Solicitação não encontrada.");
    }

    public void reject(int id) {

        List<Request> list = requestDAO.findAll();

        for (Request r : list) {

            if (r.getId() == id) {

                if (!r.getStatus().equalsIgnoreCase("PENDENTE")) {
                    System.out.println("Essa solicitação já foi analisada.");
                    return;
                }

                requestDAO.updateStatus(id, "REJEITADO");
                System.out.println("Solicitação rejeitada.");
                return;
            }
        }

        System.out.println("Solicitação não encontrada.");
    }

    public void delete(int id) {
        requestDAO.delete(id);
    }

    private boolean isValidType(String type) {

        if (type == null) return false;

        return type.equalsIgnoreCase("BONIFICACAO") ||
                type.equalsIgnoreCase("AJUDA_CUSTO") ||
                type.equalsIgnoreCase("DESCONTO") ||
                type.equalsIgnoreCase("ASO") ||
                type.equalsIgnoreCase("EPI");
    }

    private void printRequest(Request r) {
        System.out.println(
                r.getId() + " | " +
                        "Promotor: " + r.getId_Promoter() + " | " +
                        r.getType() + " | " +
                        "R$ " + r.getAmount() + " | " +
                        r.getMessage() + " | " +
                        r.getStatus() + " | " +
                        r.getDate()
        );
    }

    public List<PromoterPaymentData> getPendingPixBatch(LocalDate paymentDate) {
        List<Request> requests = requestDAO.findByStatus("PENDENTE");
        List<PromoterPaymentData> payments = new ArrayList<>();

        for (Request request : requests) {
            Promoter promoter = promoterDAO.findById(request.getId_Promoter());

            if (promoter == null) {
                continue;
            }

            if (promoter.getPix() == null || promoter.getPix().isBlank()) {
                continue;
            }

            payments.add(
                    new PromoterPaymentData(
                            promoter.getCpf(),
                            promoter.getName(),
                            promoter.getPix(),
                            promoter.getPixType(),
                            request.getAmount(),
                            paymentDate
                    )
            );
        }

        return payments;
    }
}