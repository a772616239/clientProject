/**
 * created by tool DAOGenerate
 */
package daoMaster;


import java.util.List;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import db.core.BaseDAO;
import model.bosstower.entity.bosstowerEntity;

/**
 * created by tool
 */

public interface bosstowerDAO extends BaseDAO<bosstowerEntity> {

	@Insert("INSERT INTO bosstower(playeridx,info) VALUES (#{playeridx},#{info});")
	public void insert(bosstowerEntity entity);

	@Delete("delete from bosstower where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update bosstower set playeridx=#{playeridx},info=#{info} where playeridx = #{playeridx}")
	public void update(bosstowerEntity entity);

	@Select("SELECT * FROM bosstower")
	public List<bosstowerEntity> listAll();
	
	
	@Insert("INSERT INTO bosstower(playeridx,info) VALUES (0,0);")
	public void test();


}