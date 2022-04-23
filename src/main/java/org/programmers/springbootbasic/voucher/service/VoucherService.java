package org.programmers.springbootbasic.voucher.service;

import org.programmers.springbootbasic.voucher.domain.Voucher;
import org.programmers.springbootbasic.voucher.domain.VoucherType;

import java.util.List;
import java.util.UUID;

public interface VoucherService {

    Voucher createVoucher(int amount, VoucherType voucherType);

    void registerVouchersOwner(UUID voucherId, Long MemberId);

    Voucher getVoucher(UUID voucherId);

    long applyVoucher(long beforeDiscount, Voucher voucher);

    void useVoucher(UUID voucherId);

    List<Voucher> getAllVouchers();
}
