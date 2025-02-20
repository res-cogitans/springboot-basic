package org.programmers.springbootbasic.web.dto;

import lombok.Data;
import org.programmers.springbootbasic.voucher.domain.VoucherType;

import javax.validation.constraints.NotNull;

@Data
public class VoucherCreateForm {

    @NotNull
    private VoucherType type;

    @NotNull
    private Integer amount;
}