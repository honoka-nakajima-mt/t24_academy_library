package jp.co.metateam.library.controller;
 
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.el.ELException;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;


/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();

        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);

        // 貸出一覧画面に遷移
        return "rental/index";

    }


    //貸出登録の初期表示
    @GetMapping("/rental/add")
    public String add(Model model) {
        
        //ACCOUNTテーブル、STOCKテーブルから情報を全権取得
        List<Account> accountList = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();

        //貸出登録画面に渡すデータをmodelに追加
        //プルダウンのリストをセットしmodelに追加
        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }


     //貸出登録のPost処理
     @PostMapping("/rental/add")
     public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
         try {                     
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }


            //日付妥当性チェック
            Optional<String> returnDateValidationError = rentalManageDto.validateReturnDate();
            if (returnDateValidationError.isPresent()) {
                FieldError fieldError = new FieldError("rentalManageDto", "expectedReturnOn", returnDateValidationError.get());
                result.addError(fieldError); // エラーをBindingResultに追加する
                throw new Exception(returnDateValidationError.get());  // バリデーションエラーがある場合は例外をスローする     
            }


            //貸出可否チェック
            String dateError = this.findAvailableRentalDate(rentalManageDto, rentalManageDto.getStockId());
            if (dateError != null) {
                List<RentalManage> rentalAvailable1 = this.rentalManageService.findByStockIdAndStatus1(rentalManageDto.getStockId());
                for (RentalManage list1 : rentalAvailable1) { 
                    if ((rentalManageDto.getExpectedRentalOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedRentalOn().after(list1.getRentaledAt())) ||        
                        rentalManageDto.getExpectedRentalOn().before(list1.getExpectedReturnOn())) {
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));
                        throw new Exception(dateError);
                    } else if(
                         (rentalManageDto.getExpectedReturnOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedReturnOn().after(list1.getRentaledAt())) ||     
                         rentalManageDto.getExpectedReturnOn().before(list1.getExpectedReturnOn())) {
                         result.addError(new FieldError("rentalManageDto", "expectedReturnOn", dateError));
                         throw new Exception(dateError);
                    }
                }
                List<RentalManage> rentalAvailable0 = this.rentalManageService.findByStockIdAndStatus0(rentalManageDto.getStockId());
                for (RentalManage list0 : rentalAvailable0) { 
                    if (!(rentalManageDto.getExpectedRentalOn().before(list0.getExpectedRentalOn()) ||     //貸出予定日がリストの貸出予定日より後だとエラー
                        rentalManageDto.getExpectedRentalOn().after(list0.getExpectedReturnOn()))) {     //貸出予定日がリストの返却予定日より後だとエラー
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));      //貸出の下にエラー出す
                        throw new Exception(dateError);
                    } else if(!
                        (rentalManageDto.getExpectedReturnOn().before(list0.getExpectedRentalOn()) ||     //返却予定日がリストの貸出予定日より後だとエラー
                        rentalManageDto.getExpectedReturnOn().after(list0.getExpectedReturnOn()))) {    //返却予定日がリストの返却予定日より前だとエラー
                        result.addError(new FieldError("rentalManageDto", "expectedReturnOn", dateError));  //返却の下にエラー出す
                        throw new Exception(dateError);
                    }
                }       
            }

            
             // 登録処理
             this.rentalManageService.save(rentalManageDto);
            
             return "redirect:/rental/index";
         } catch (Exception e) {
             log.error(e.getMessage());

             ra.addFlashAttribute("rentalManageDto", rentalManageDto);
             ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

         return "redirect:/rental/add";
        }
     }


     //貸出編集の初期表示
     @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accountList = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));   //valueOf→数値を文字列へ変換

            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());       //getAccountの中のgetEmployeeIdを取り出す
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());


            model.addAttribute("rentalManageDto", rentalManageDto);
            
        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }


        //日付妥当性チェック
        Optional<String> returnDateValidationError = rentalManageDto.validateReturnDate();
        if (returnDateValidationError.isPresent()) {
            FieldError fieldError = new FieldError("rentalManageDto", "expectedReturnOn", returnDateValidationError.get());
            result.addError(fieldError); // エラーをBindingResultに追加する
            throw new Exception(returnDateValidationError.get());  // バリデーションエラーがある場合は例外をスローする              
        }

        Optional<String> rentalDateValidationError = rentalManageDto.validateRentalDate();
        if (rentalDateValidationError.isPresent()) {
            FieldError fieldError = new FieldError("rentalManageDto", "expectedRentalOn", rentalDateValidationError.get());
            result.addError(fieldError); // エラーをBindingResultに追加する
            throw new Exception(rentalDateValidationError.get());  // バリデーションエラーがある場合は例外をスローする              
        }
        

        // 前の貸出ステータスを取得
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id)); 
        Integer previousRentalStetas = rentalManage.getStatus();

        // バリデーションチェック
        Optional<String> validationError = rentalManageDto.isValidRentalStatus(previousRentalStetas);
        if (validationError.isPresent()) {
            // バリデーションエラーがある場合、特定のフィールドに関連付けられたエラーメッセージを作成する
            FieldError fieldError = new FieldError("rentalManageDto", "status", validationError.get());
            result.addError(fieldError); // エラーをBindingResultに追加する
            throw new Exception(validationError.get());  // バリデーションエラーがある場合は例外をスローする              
        }


        //貸出可否チェック
        String dateError = this.findAvailableRentalDate(rentalManageDto, rentalManageDto.getStockId(), rentalManageDto.getId());
            if (dateError != null) {
                List<RentalManage> rentalAvailable1 = this.rentalManageService.findByStockIdAndStatus1(rentalManageDto.getStockId());
                for (RentalManage list1 : rentalAvailable1) { 
                    if ((rentalManageDto.getExpectedRentalOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedRentalOn().after(list1.getRentaledAt())) ||        
                        rentalManageDto.getExpectedRentalOn().before(list1.getExpectedReturnOn())) {
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));
                        throw new Exception(dateError);
                    } else if(
                         (rentalManageDto.getExpectedReturnOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedReturnOn().after(list1.getRentaledAt())) ||     
                         rentalManageDto.getExpectedReturnOn().before(list1.getExpectedReturnOn())) {
                         result.addError(new FieldError("rentalManageDto", "expectedReturnOn", dateError));
                         throw new Exception(dateError);
                    }
                }         
                List<RentalManage> rentalAvailable0 = this.rentalManageService.findByStockIdAndStatus0(rentalManageDto.getStockId());
                for (RentalManage list0 : rentalAvailable0) { 
                    if (!(rentalManageDto.getExpectedRentalOn().before(list0.getExpectedRentalOn()) ||     //貸出予定日がリストの貸出予定日より後だとエラー
                        rentalManageDto.getExpectedRentalOn().after(list0.getExpectedReturnOn()))) {     //貸出予定日がリストの返却予定日より後だとエラー
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));      //貸出の下にエラー出す
                        throw new Exception(dateError);
                    } else if(!
                        (rentalManageDto.getExpectedReturnOn().before(list0.getExpectedRentalOn()) ||     //返却予定日がリストの貸出予定日より後だとエラー
                        rentalManageDto.getExpectedReturnOn().after(list0.getExpectedReturnOn()))) {    //返却予定日がリストの返却予定日より前だとエラー
                        result.addError(new FieldError("rentalManageDto", "expectedReturnOn", dateError));  //返却の下にエラー出す
                        throw new Exception(dateError);
                    }
                } 
            }

        

         
            // 更新処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/{id}/edit";
        }
    }


    //貸出登録の可否チェックメソッド
    //在庫管理番号に関連付けられた貸出情報を取得し、RentalManageDtoと比較して、重複する貸出期間があるかどうかをチェックする
    public String findAvailableRentalDate(RentalManageDto rentalManageDto, String StockId) {
        List<RentalManage> rentalAvailable1 = this.rentalManageService.findByStockIdAndStatus1(StockId);
        for (RentalManage list1 : rentalAvailable1) {
            if (rentalManageDto.getExpectedRentalOn().after(list1.getExpectedRentalOn())|| rentalManageDto.getExpectedRentalOn().after(list1.getRentaledAt()) &&     
                rentalManageDto.getExpectedRentalOn().before(list1.getExpectedReturnOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
            }
            if (rentalManageDto.getExpectedReturnOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedReturnOn().after(list1.getRentaledAt()) &&
                rentalManageDto.getExpectedReturnOn().before(list1.getExpectedReturnOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
            }
        }
        List<RentalManage> rentalAvailable0 = this.rentalManageService.findByStockIdAndStatus0(StockId);
        for (RentalManage list0 : rentalAvailable0) {
            if (rentalManageDto.getExpectedRentalOn().before(list0.getExpectedReturnOn()) && 
                rentalManageDto.getExpectedReturnOn().after(list0.getExpectedRentalOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
        }
        }
        return null;
}



    //貸出編集の可否チェックメソッド
    //在庫管理番号に関連付けられた貸出情報を取得し、RentalManageDtoと比較して、重複する貸出期間があるかどうかをチェックする
    public String findAvailableRentalDate(RentalManageDto rentalManageDto, String StockId, Long retalId){    
        List<RentalManage> rentalAvailable1 = this.rentalManageService.findByStockIdAndRentalIdAndStatus1(StockId, retalId);
        for (RentalManage list1 : rentalAvailable1) {
            if (rentalManageDto.getExpectedRentalOn().after(list1.getExpectedRentalOn())|| rentalManageDto.getExpectedRentalOn().after(list1.getRentaledAt()) &&     
                  rentalManageDto.getExpectedRentalOn().before(list1.getExpectedReturnOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
            }
            if (rentalManageDto.getExpectedReturnOn().after(list1.getExpectedRentalOn()) || rentalManageDto.getExpectedReturnOn().after(list1.getRentaledAt()) &&
                  rentalManageDto.getExpectedReturnOn().before(list1.getExpectedReturnOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
            }
        }

        List<RentalManage> rentalAvailable0 =this.rentalManageService.findByStockIdAndRentalIdAndStatus0(StockId,retalId);
        for (RentalManage list0 : rentalAvailable0) {
            if (rentalManageDto.getExpectedRentalOn().before(list0.getExpectedReturnOn()) && 
                rentalManageDto.getExpectedReturnOn().after(list0.getExpectedRentalOn())) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
            }
        }
        return null;
    }
}

