package dev.distributed.bank.service;

import dev.distributed.bank.distributed.SiteRouter;
import dev.distributed.bank.dto.request.CreateCustomerRequest;
import dev.distributed.bank.entity.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    private final SiteRouter siteRouter;

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setBranchId(rs.getString("branch_id"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return c;
    };

    public CustomerService(SiteRouter siteRouter) {
        this.siteRouter = siteRouter;
    }

    public List<Customer> getCustomersByBranch(String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        return jdbc.query("SELECT * FROM customer ORDER BY customer_id", customerRowMapper);
    }

    public List<Customer> getAllCustomers() {
        List<Customer> all = new ArrayList<>();
        for (String branchId : siteRouter.getAllBranchIds()) {
            try {
                JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
                List<Customer> customers = jdbc.query(
                        "SELECT * FROM customer ORDER BY customer_id", customerRowMapper);
                all.addAll(customers);
            } catch (Exception e) {
                System.out.println("⚠️ Cannot reach site " + branchId + ": " + e.getMessage());
            }
        }
        return all;
    }

    public Customer getCustomerById(Long id, String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        List<Customer> customers = jdbc.query(
                "SELECT * FROM customer WHERE customer_id = ?",
                customerRowMapper, id);
        return customers.isEmpty() ? null : customers.get(0);
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(request.getBranchId());

        // Dùng KeyHolder để lấy auto-generated ID sau khi INSERT
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO customer (full_name, phone, email, address, branch_id) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getFullName());
            ps.setString(2, request.getPhone());
            ps.setString(3, request.getEmail());
            ps.setString(4, request.getAddress());
            ps.setString(5, request.getBranchId().toUpperCase());
            return ps;
        }, keyHolder);

        // Lấy KH vừa tạo để trả về
        Long newId = keyHolder.getKey().longValue();
        return getCustomerById(newId, request.getBranchId());
    }

    public Customer updateCustomer(Long id, CreateCustomerRequest request) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(request.getBranchId());
        jdbc.update(
                "UPDATE customer SET full_name = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?",
                request.getFullName(), request.getPhone(), request.getEmail(),
                request.getAddress(), id);
        return getCustomerById(id, request.getBranchId());
    }

    public void deleteCustomer(Long id, String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        jdbc.update("DELETE FROM customer WHERE customer_id = ?", id);
    }
}
