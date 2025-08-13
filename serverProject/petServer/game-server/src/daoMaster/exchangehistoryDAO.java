/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.exchangehistory.entity.exchangehistoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface exchangehistoryDAO extends BaseDAO<exchangehistoryEntity> {

	@Insert("INSERT INTO exchangehistory(idx,playeridx,exchangehistory) VALUES (#{idx},#{playeridx},#{exchangehistory});")
	public void insert(exchangehistoryEntity entity);

	@Delete("delete from exchangehistory where idx = #{idx}")
	public void delete(String idx);

	@Update("update exchangehistory set idx=#{idx},playeridx=#{playeridx},exchangehistory=#{exchangehistory} where idx = #{idx}")
	public void update(exchangehistoryEntity entity);

	@Select("SELECT * FROM exchangehistory")
	public List<exchangehistoryEntity> listAll();
	
	
	@Insert("INSERT INTO exchangehistory(idx,playeridx,exchangehistory) VALUES (0,0,0);")
	public void test();


}