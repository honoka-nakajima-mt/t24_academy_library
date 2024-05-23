package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.controller.RentalManageController;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.text.ParseException;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;


    
    public Optional<String> validateReturnDate() {
        if (this.expectedRentalOn != null && this.expectedReturnOn != null) {
            if (this.expectedReturnOn.before(this.expectedRentalOn)) {
                return Optional.of("返却予定日は貸出予定日より後の日付を入力してください");
            }
        }
        return Optional.empty();
    }

    public Optional<String> validateRentalDate() {
        Date currentDate = new Date(); // 現在の日時を取得
        if (this.expectedRentalOn.equals(currentDate) || this.expectedRentalOn.after(currentDate)) {
            return Optional.empty();
        } else {
            return Optional.of("貸出予定日は現在の日時より後の日付を入力してください");
        }
    }
    

    public Optional<String> isValidRentalStatus(Integer previousRentalStetas) {  

        if (previousRentalStetas == RentalStatus.RENT_WAIT.getValue() && this.status != previousRentalStetas) {
            String currentStatusText = RentalStatus.getText(this.status);
            // 貸出ステータスが返却済みになっている場合
            if (this.status == RentalStatus.RETURNED.getValue()) {
                return Optional.of("貸出ステータスは貸出待ちから" + currentStatusText + "に変更できません");
            }
        } else if (previousRentalStetas == RentalStatus.RENTAlING.getValue() && this.status != previousRentalStetas) {
            String currentStatusText = RentalStatus.getText(this.status);
            // 貸出ステータスが返却済み以外になっている場合
            if (this.status != RentalStatus.RETURNED.getValue()) {
                return Optional.of("貸出ステータスは貸出中から" + currentStatusText + "に変更できません");
            }
        } else if (previousRentalStetas == RentalStatus.RETURNED.getValue() && this.status != previousRentalStetas) {
            String currentStatusText = RentalStatus.getText(this.status);
            return Optional.of("貸出ステータスは返却済みから" + currentStatusText + "に変更できません");         // 貸出ステータスが返却済みから変更されている場合
        } else if (previousRentalStetas == RentalStatus.CANCELED.getValue() && this.status != previousRentalStetas) {
            String currentStatusText = RentalStatus.getText(this.status);
            return Optional.of("貸出ステータスはキャンセルから" + currentStatusText + "に変更できません");             // 貸出ステータスがキャンセルから変更されている場合
        }

        // すべての条件を満たしていない場合に変更できる
        return Optional.empty();
    }
}