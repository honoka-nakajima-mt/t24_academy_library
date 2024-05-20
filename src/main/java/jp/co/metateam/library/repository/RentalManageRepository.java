package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);




    //貸出登録での貸出可否チェック用のリスト
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
    ("SELECT rm FROM RentalManage rm " +                //SQLでいう　SELECT * FROM RentalManage　と同義
    " WHERE(rm.status=0) " +                            //貸出ステータスが０で
    " AND ?1 = rm.stock.id ")                           //かつ在庫管理番号が一致しているもの
    List<RentalManage> findByStockIdAndStatus0(String StockId);   //StockIdが指定された貸出管理番号に一致する貸出情報を検索してリストとして返す

    //貸出登録での貸出可否チェック用のリスト
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
    ("SELECT rm FROM RentalManage rm " +                //SQLでいう　SELECT * FROM RentalManage　と同義
    " WHERE(rm.status=1) " +                            //貸出ステータスが１で
    " AND ?1 = rm.stock.id ")                           //かつ在庫管理番号が一致しているもの
    List<RentalManage> findByStockIdAndStatus1(String StockId);   //StockIdが指定された貸出管理番号に一致する貸出情報を検索してリストとして返す

    //貸出編集での貸出可否チェック用のリスト取得
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     ("SELECT rm FROM RentalManage rm " +               //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=0) " +                          //貸出ステータスが０で
      " AND ?1 = rm.stock.id " +                        //かつ在庫管理番号が一致しているもの
      " AND ?2 <> rm.id ")                              //かつ貸出管理番号が一致していないもの
    List<RentalManage> findByStockIdAndRentalIdAndStatus0(String StockId, Long retalId);     //StockIdとrentalIdの両方が指定された条件に一致する貸出情報を検索してリストとして返す

    //貸出編集での貸出可否チェック用のリスト取得
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     ("SELECT rm FROM RentalManage rm " +               //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=1) " +                          //貸出ステータスが１で
      " AND ?1 = rm.stock.id " +                        //かつ在庫管理番号が一致しているもの
      " AND ?2 <> rm.id ")                              //かつ貸出管理番号が一致していないもの
    List<RentalManage> findByStockIdAndRentalIdAndStatus1(String StockId, Long retalId);     //StockIdとrentalIdの両方が指定された条件に一致する貸出情報を検索してリストとして返す
}
