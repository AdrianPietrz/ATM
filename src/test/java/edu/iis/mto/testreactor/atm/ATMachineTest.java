package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.iis.mto.testreactor.atm.bank.AccountException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {

    @Mock
    Bank bank;

    private PinCode pin;
    private Card card;
    private ATMachine atMachine;
    private List<BanknotesPack> banknotesPacks = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        pin = PinCode.createPIN(0,0,0,0);
        card = Card.create("1234");

        Currency currency = Currency.getInstance("PLN");
        atMachine = new ATMachine(bank, currency);
        banknotesPacks.add(BanknotesPack.create(10,Banknote.PL_100));
        banknotesPacks.add(BanknotesPack.create(20,Banknote.PL_10));
        MoneyDeposit moneyDeposit = MoneyDeposit.create(currency,banknotesPacks);
        atMachine.setDeposit(moneyDeposit);
    }

    @Test
    void methodShouldThrowErrorWrongCurrencyWhenWrongCurrencyWithdrawn() throws ATMOperationException {
        ATMOperationException exception = null;
        try{
            atMachine.withdraw(pin,card,new Money(10,"USD"));
        } catch (ATMOperationException e){
            exception = e;

        }
        assertEquals(ErrorCode.WRONG_CURRENCY, exception.getErrorCode());

    }

    @Test
    void methodShouldThrowErrorAuthorizationWhenAuthorizationFails() throws AuthorizationException {
        ATMOperationException exception = null;

        when(bank.autorize(any(),any())).thenThrow(AuthorizationException.class);

        try{
            atMachine.withdraw(pin,card,new Money(10,"PLN"));
        } catch (ATMOperationException e){
            exception = e;
        }
        assertEquals(ErrorCode.AHTHORIZATION, exception.getErrorCode());

    }

    @Test
    void methodShouldThrowErrorWrongAmountWhenAmountToWithdrawIsToBig(){
        ATMOperationException exception = null;

        try{
            atMachine.withdraw(pin,card,new Money(1500,"PLN"));
        } catch (ATMOperationException e){
            exception = e;
        }
        assertEquals(ErrorCode.WRONG_AMOUNT, exception.getErrorCode());
    }

    @Test
    void methodShouldThrowErrorNoFoundsOnAccountWhenThereIsNotEnoughFoundsOnAccount() throws AccountException {
        ATMOperationException exception = null;

        doThrow(AccountException.class).when(bank).charge(any(),any());

        try{
            atMachine.withdraw(pin,card,new Money(1000,"PLN"));
        } catch (ATMOperationException e){
            exception = e;
        }
        assertEquals(ErrorCode.NO_FUNDS_ON_ACCOUNT, exception.getErrorCode());
    }

    @Test
    void methodShouldThrowErrorWrongAmountWhenNotCorrectAmountToWithdrawPassed(){
        ATMOperationException exception = null;

        try{
            atMachine.withdraw(pin,card,new Money(500.5,"PLN"));
        } catch (ATMOperationException e){
            exception = e;
        }
        assertEquals(ErrorCode.WRONG_AMOUNT, exception.getErrorCode());
    }

    @Test
    void methodShouldCallAuthorizationOnceWhenWithdrawing() throws ATMOperationException, AuthorizationException {

        atMachine.withdraw(pin,card,new Money(100,"PLN"));

        verify(bank,times(1)).autorize(any(),any());
    }


}
