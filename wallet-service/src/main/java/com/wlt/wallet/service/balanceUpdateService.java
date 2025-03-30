package com.wlt.wallet.service;

import com.wlt.wallet.dto.BalanceUpdateEvent;
import com.wlt.wallet.dto.CreditAccountBalanceRequestDto;
import lombok.Data;

public interface balanceUpdateService {
    void updateBalance(BalanceUpdateEvent event);
}
