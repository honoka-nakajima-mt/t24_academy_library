package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CalendarDto {

    private String title;

    private Long stockNum;

    private Date expectedRentalOn;

    private String stockId;

    private Object dayStockNum;
}