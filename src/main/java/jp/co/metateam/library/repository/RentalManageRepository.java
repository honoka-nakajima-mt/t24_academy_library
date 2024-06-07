package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

import java.util.Date;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	  Optional<RentalManage> findById(Long id);


    //貸出登録での貸出可否チェック用のリスト
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     (value = 
      "SELECT rm FROM RentalManage rm " +                //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=0) " +                            //貸出ステータスが０で
      " AND ?1 = rm.stock.id ")                           //かつ在庫管理番号が一致しているもの
    List<RentalManage> findByStockIdAndStatus0(String StockId);   //StockIdが指定された貸出管理番号に一致する貸出情報を検索してリストとして返す

    //貸出登録での貸出可否チェック用のリスト
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     (value = 
      "SELECT rm FROM RentalManage rm " +                //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=1) " +                            //貸出ステータスが１で
      " AND ?1 = rm.stock.id ")                           //かつ在庫管理番号が一致しているもの
    List<RentalManage> findByStockIdAndStatus1(String StockId);   //StockIdが指定された貸出管理番号に一致する貸出情報を検索してリストとして返す

    //貸出編集での貸出可否チェック用のリスト取得
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     (value = 
      "SELECT rm FROM RentalManage rm " +               //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=0) " +                          //貸出ステータスが０で
      " AND ?1 = rm.stock.id " +                        //かつ在庫管理番号が一致しているもの
      " AND ?2 <> rm.id ")                              //かつ貸出管理番号が一致していないもの
    List<RentalManage> findByStockIdAndRentalIdAndStatus0(String StockId, Long retalId);     //StockIdとrentalIdの両方が指定された条件に一致する貸出情報を検索してリストとして返す

    //貸出編集での貸出可否チェック用のリスト取得
    @Query                                              //@Queryアノテーションは、Spring Data JPA で定義されたクエリメソッドを表している
     (value = 
      "SELECT rm FROM RentalManage rm " +               //SQLでいう　SELECT * FROM RentalManage　と同義
      " WHERE(rm.status=1) " +                          //貸出ステータスが１で
      " AND ?1 = rm.stock.id " +                        //かつ在庫管理番号が一致しているもの
      " AND ?2 <> rm.id ")                              //かつ貸出管理番号が一致していないもの
    List<RentalManage> findByStockIdAndRentalIdAndStatus1(String StockId, Long retalId);     //StockIdとrentalIdの両方が指定された条件に一致する貸出情報を検索してリストとして返す

    @Query
     (value = 
      "SELECT COUNT(*) AS count " +
      "FROM rental_manage rm " +
      "JOIN stocks s ON rm.stock_id = s.id " +
      "JOIN book_mst bm ON s.book_id = bm.id " +
      "WHERE rm.expected_rental_on <= :day AND :day <= rm.expected_return_on AND bm.title = :title", nativeQuery = true)
    Long findByUnAvailableCount(@Param("day") Date day, @Param("title") String title);    //貸出予定日が指定された日付（day）と返却予定日が指定された日付（day）の間にある、指定された書籍の貸出数をカウント

    @Query
     (value = 
      "SELECT st.id " +
      "FROM stocks st " +
      "JOIN book_mst bm ON st.book_id = bm.id " +
      "LEFT JOIN rental_manage rm ON st.id = rm.stock_id " +
      "WHERE (rm.expected_rental_on > :day OR rm.expected_return_on < :day OR rm.stock_id IS NULL) " +
      "AND bm.title = :title " +
      "AND st.status = '0' ", nativeQuery = true)
    List<String> findByAvailableStockId(@Param("day") Date day, @Param("title") String title);    //指定された日付（day）と書籍タイトル（title）に基づいて、利用可能な在庫IDのリストを取得
}
