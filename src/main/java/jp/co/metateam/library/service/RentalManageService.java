package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;


@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }

    @Transactional 
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            RentalManage rentalManage = findById(Long.valueOf(id));  //DBに登録されているデータを持ってきている
            if (rentalManage == null){
                throw new Exception("Id not found.");
            }

            Account account = rentalManage.getAccount();
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = rentalManage.getStock();
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }


    //貸出登録での貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatus0(String StockId){        //指定された在庫管理番号に関連付けられたすべての貸出情報を取得
        List<RentalManage> rentalAvailable0 =this.rentalManageRepository.findByStockIdAndStatus0(StockId);      //与えられた在庫管理番号に基づいて貸出情報を検索する
        return rentalAvailable0;        
    }

    //貸出登録での貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatus1(String StockId){        //指定された在庫管理番号に関連付けられたすべての貸出情報を取得
        List<RentalManage> rentalAvailable1 =this.rentalManageRepository.findByStockIdAndStatus1(StockId);      //与えられた在庫管理番号に基づいて貸出情報を検索する
        return rentalAvailable1;        
    }

    //貸出編集の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndRentalIdAndStatus0(String StockId, Long retalId){       //指定されたの在庫管理番号と貸出管理番号に関連付けられたすべての貸出情報を取得
        List<RentalManage> rentalAvailable0 =this.rentalManageRepository.findByStockIdAndRentalIdAndStatus0(StockId,retalId);      //与えられた在庫管理番号と貸出管理番号の両方を使用して貸出情報を検索する
        return rentalAvailable0;
    }

    //貸出編集の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndRentalIdAndStatus1(String StockId, Long retalId){       //指定されたの在庫管理番号と貸出管理番号に関連付けられたすべての貸出情報を取得
        List<RentalManage> rentalAvailable1 =this.rentalManageRepository.findByStockIdAndRentalIdAndStatus1(StockId,retalId);      //与えられた在庫管理番号と貸出管理番号の両方を使用して貸出情報を検索する
        return rentalAvailable1;
    }
}