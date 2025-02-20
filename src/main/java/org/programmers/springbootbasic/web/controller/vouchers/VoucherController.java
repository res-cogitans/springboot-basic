package org.programmers.springbootbasic.web.controller.vouchers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.springbootbasic.voucher.domain.VoucherProperty;
import org.programmers.springbootbasic.voucher.domain.VoucherType;
import org.programmers.springbootbasic.voucher.repository.VoucherRepository;
import org.programmers.springbootbasic.voucher.service.VoucherService;
import org.programmers.springbootbasic.web.dto.VoucherCreateForm;
import org.programmers.springbootbasic.web.dto.VoucherDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.programmers.springbootbasic.voucher.domain.VoucherType.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    private final VoucherProperty voucherProperty;

    private static final VoucherType[] VOUCHER_TYPES = values();

    @ModelAttribute("voucherTypes")
    public VoucherType[] voucherTypes() {
        return VOUCHER_TYPES;
    }

    @GetMapping("voucher")
    public String createForm(Model model) {
        var voucher = new VoucherCreateForm();
        model.addAttribute("voucher", voucher);
        return "vouchers/createVoucher";
    }

    @PostMapping("voucher")
    public String createVoucher(@Valid @ModelAttribute("voucher") VoucherCreateForm form,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (isFormInputValid(bindingResult.hasErrors(), form.getAmount(), form.getType())) {
            var createdVoucher = voucherService.createVoucher(form.getAmount(), form.getType());

            redirectAttributes.addAttribute("voucherId", createdVoucher.getId());
            return "redirect:/vouchers/{voucherId}";
        }

        return redirectToForm(form, bindingResult);
    }

    private boolean isFormInputValid(boolean hasError, Integer amount, VoucherType type) {
        return !hasError && (amount != null && type != null) && voucherService.isValidAmount(amount, type);
    }

    private String redirectToForm(VoucherCreateForm form, BindingResult bindingResult) {
        if (form.getType() != null || form.getAmount() != null) {
            int minimumAmount = 0;
            int maximumAmount = 0;
            if (form.getType() == FIXED) {
                minimumAmount = voucherProperty.getFixed().minimumAmount();
                maximumAmount = voucherProperty.getFixed().maximumAmount();
            }
            if (form.getType() == RATE) {
                minimumAmount = voucherProperty.getRate().minimumAmount();
                maximumAmount = voucherProperty.getRate().maximumAmount();
            }

            bindingResult.reject("form.voucher.value", new Object[]{form.getType().getName(), minimumAmount, maximumAmount}, "부적합한 값입니다.");
        }
        return "vouchers/createVoucher";
    }

    @GetMapping("vouchers/{voucherId}")
    public String getVoucher(@PathVariable("voucherId") UUID voucherId, Model model) {
        VoucherDto voucher = VoucherDto.from(voucherService.getVoucher(voucherId));
        model.addAttribute("voucher", voucher);

        return "vouchers/editVoucher";
    }

    @PostMapping("vouchers/{voucherId}/delete")
    public String deleteVoucher(@PathVariable("voucherId") UUID voucherId) {
        log.info("deleteRequest for voucherId = {}", voucherId);
        voucherService.deleteVoucher(voucherId);
        return "redirect:/vouchers";
    }

    @GetMapping("vouchers")
    public String voucherList(Model model, @RequestParam(required = false) VoucherType type,
                              @RequestParam(required = false) Date startingDate, @RequestParam(required = false) Date endingDate) {

        model.addAttribute("startingDate", startingDate);
        model.addAttribute("endingDate", endingDate);

        if (type != null && isSearchDateValid(startingDate, endingDate)) {
            return (startingDate != null && endingDate != null) ?
                    voucherListByTypeAndDate(model, type, startingDate, endingDate)
                    : voucherListByType(model, type);
        }
        if (startingDate != null && endingDate != null) {
            return voucherListByDate(model, startingDate, endingDate);
        }

        if (!isSearchDateValid(startingDate, endingDate)) {
            model.addAttribute("dateParameterError", true);
        }

        return voucherListWithoutConditional(model);
    }

    private boolean isSearchDateValid(Date startingDate, Date endingDate) {
        return !(startingDate != null && endingDate != null && startingDate.after(endingDate));
    }

    private String voucherListWithoutConditional(Model model) {
        List<VoucherDto> vouchers = new ArrayList<>();
        voucherService.getAllVouchers().forEach(voucher -> vouchers.add(VoucherDto.from(voucher)));
        model.addAttribute("vouchers", vouchers);
        return "vouchers/voucherList";
    }

    private String voucherListByTypeAndDate(Model model, VoucherType type, Date startingDate, Date endingDate) {
        model.addAttribute("type", type);
        model.addAttribute("startingDate", startingDate);
        model.addAttribute("endingDate", endingDate);
        List<VoucherDto> vouchers = voucherService.getVouchersByTypeAndDate(type, startingDate, endingDate)
                .stream().map(VoucherDto::from).toList();
        model.addAttribute("vouchers", vouchers);
        return "vouchers/voucherList";
    }

    private String voucherListByType(Model model, VoucherType type) {
        model.addAttribute("type", type);
        List<VoucherDto> vouchers = voucherService.getVouchersByType(type)
                .stream().map(VoucherDto::from).toList();
        model.addAttribute("vouchers", vouchers);
        return "vouchers/voucherList";
    }

    private String voucherListByDate(Model model, Date startingDate, Date endingDate) {
        model.addAttribute("startingDate", startingDate);
        model.addAttribute("endingDate", endingDate);
        List<VoucherDto> vouchers = voucherService.getVouchersByDate(startingDate, endingDate)
                .stream().map(VoucherDto::from).toList();
        model.addAttribute("vouchers", vouchers);
        return "vouchers/voucherList";
    }
}