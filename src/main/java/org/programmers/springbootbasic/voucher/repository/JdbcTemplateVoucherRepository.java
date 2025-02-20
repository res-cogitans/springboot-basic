package org.programmers.springbootbasic.voucher.repository;

import lombok.extern.slf4j.Slf4j;
import org.programmers.springbootbasic.voucher.domain.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.programmers.springbootbasic.voucher.converter.UuidByteArrayConverter.bytesToUuid;
import static org.programmers.springbootbasic.voucher.converter.UuidByteArrayConverter.uuidToBytes;
import static org.programmers.springbootbasic.voucher.domain.VoucherType.FIXED;
import static org.programmers.springbootbasic.voucher.domain.VoucherType.RATE;

@Slf4j
@Repository
public class JdbcTemplateVoucherRepository implements VoucherRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public static final String PARAM_KEY_VOUCHER_ID = "voucherId";
    public static final String PARAM_KEY_AMOUNT = "amount";
    public static final String PARAM_KEY_TYPE = "type";
    public static final String PARAM_KEY_REGISTERED_AT = "registeredAt";
    public static final String PARAM_KEY_MEMBER_ID = "memberId";
    public static final String PARAM_KEY_STARTING_DATE = "startingDate";
    public static final String PARAM_KEY_ENDING_DATE = "endingDate";

    private static final String INSERT_SQL =
            "INSERT into voucher(voucher_id, amount, type, registered_at) values (:"
                    + PARAM_KEY_VOUCHER_ID + ", :" + PARAM_KEY_AMOUNT + ", :" + PARAM_KEY_TYPE + ", :" + PARAM_KEY_REGISTERED_AT + ")";
    private static final String UPDATE_MEMBER_FK_SQL =
            "UPDATE voucher SET member_id = :" + PARAM_KEY_MEMBER_ID + " WHERE voucher_id = :" + PARAM_KEY_VOUCHER_ID;
    private static final String FIND_BY_ID_SQL =
            "SELECT * from voucher WHERE voucher_id = :" + PARAM_KEY_VOUCHER_ID;
    private static final String FIND_BY_TYPE_SQL =
            "SELECT * from voucher WHERE type = :" + PARAM_KEY_TYPE + " ORDER BY registered_at DESC";
    private static final String FIND_BY_DATE_SQL = "SELECT * from voucher WHERE registered_at BETWEEN :"
            + PARAM_KEY_STARTING_DATE + " AND :" + PARAM_KEY_ENDING_DATE + " ORDER BY registered_at DESC";
    private static final String FIND_BY_TYPE_AND_DATE_SQL = "SELECT * from voucher WHERE type = :" + PARAM_KEY_TYPE
            + " AND registered_at BETWEEN :" + PARAM_KEY_STARTING_DATE + " AND :" + PARAM_KEY_ENDING_DATE + " ORDER BY registered_at DESC";
    private static final String FIND_ALL_SQL = "SELECT * from voucher ORDER BY registered_at DESC";
    private static final String REMOVE_SQL = "DELETE from voucher WHERE voucher_id = :" + PARAM_KEY_VOUCHER_ID;

    public JdbcTemplateVoucherRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Voucher insert(Voucher voucher) {
        int updatedRow = jdbcTemplate.update(INSERT_SQL, toParamMap(voucher));
        if (1 != updatedRow) {
            throw new IncorrectResultSizeDataAccessException(updatedRow);
        }
        return voucher;
    }

    private SqlParameterSource toParamMap(Voucher voucher) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue(PARAM_KEY_VOUCHER_ID, uuidToBytes(voucher.getId()));
        paramSource.addValue(PARAM_KEY_AMOUNT, voucher.getAmount());
        paramSource.addValue(PARAM_KEY_TYPE, voucher.getType().toString());
        paramSource.addValue(PARAM_KEY_REGISTERED_AT, voucher.getRegisteredAt());

        return paramSource;
    }

    @Override
    public void updateVoucherOwner(UUID voucherId, Long memberId) {
        var paramSource = new MapSqlParameterSource();
        paramSource.addValue(PARAM_KEY_VOUCHER_ID, uuidToBytes(voucherId));
        paramSource.addValue(PARAM_KEY_MEMBER_ID, memberId);

        int updatedRow = jdbcTemplate.update(UPDATE_MEMBER_FK_SQL, paramSource);

        if (1 != updatedRow) {
            throw new IncorrectResultSizeDataAccessException(updatedRow);
        }
    }

    @Override
    public Optional<Voucher> findById(UUID voucherId) {
        var paramSource = new MapSqlParameterSource(PARAM_KEY_VOUCHER_ID, uuidToBytes(voucherId));
        try {
            return Optional.of(jdbcTemplate.queryForObject(FIND_BY_ID_SQL, paramSource, voucherRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Voucher> findByType(VoucherType type) {
        var paramSource = new MapSqlParameterSource(PARAM_KEY_TYPE, type.toString());
        return jdbcTemplate.query(FIND_BY_TYPE_SQL, paramSource, voucherRowMapper());
    }

    @Override
    public List<Voucher> findByDate(Date startingDate, Date endingDate) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue(PARAM_KEY_STARTING_DATE, startingDate);
        paramSource.addValue(PARAM_KEY_ENDING_DATE, endingDate);
        return jdbcTemplate.query(FIND_BY_DATE_SQL, paramSource, voucherRowMapper());
    }

    @Override
    public List<Voucher> findByTypeAndDate(VoucherType type, Date startingDate, Date endingDate) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue(PARAM_KEY_TYPE, type.toString());
        paramSource.addValue(PARAM_KEY_STARTING_DATE, startingDate);
        paramSource.addValue(PARAM_KEY_ENDING_DATE, endingDate);
        return jdbcTemplate.query(FIND_BY_TYPE_AND_DATE_SQL, paramSource, voucherRowMapper());
    }

    private RowMapper<Voucher> voucherRowMapper() {
        return (rs, rowNum) -> {
            UUID voucherId = bytesToUuid(rs.getBytes("voucher_id"));
            int amount = rs.getInt("amount");
            String type = rs.getString("type");
            Long memberId = (Long) rs.getObject("member_id");
            Timestamp registered_at = rs.getTimestamp("registered_at");

            if (FIXED.toString().equals(type)) {
                return new FixedDiscountVoucher(voucherId, amount, memberId, registered_at);
            } else if (RATE.toString().equals(type)) {
                return new RateDiscountVoucher(voucherId, amount, memberId, registered_at);
            }
            throw new IllegalVoucherTypeException("잘못된 바우처 타입입니다. type=" + type);
        };
    }

    @Override
    public List<Voucher> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, voucherRowMapper());
    }

    @Override
    public void remove(UUID voucherId) {
        var paramSource = new MapSqlParameterSource(PARAM_KEY_VOUCHER_ID, uuidToBytes(voucherId));
        int deletedRow = jdbcTemplate.update(REMOVE_SQL, paramSource);
        if (deletedRow != 1) {
            throw new IllegalStateException("바우처가 정상적으로 삭제되지 않았습니다. deletedRow=" + deletedRow);
        }
    }
}