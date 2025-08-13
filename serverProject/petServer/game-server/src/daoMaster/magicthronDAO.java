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
import model.magicthron.entity.magicthronEntity;

/**
 * created by tool
 */

public interface magicthronDAO extends BaseDAO<magicthronEntity> {

	@Insert("INSERT INTO magicthron(idx,info) VALUES (#{idx},#{info});")
	public void insert(magicthronEntity entity);

	@Delete("delete from magicthron where idx = #{idx}")
	public void delete(String idx);

	@Update("update magicthron set idx=#{idx},info=#{info} where idx = #{idx}")
	public void update(magicthronEntity entity);

	@Select("SELECT * FROM magicthron")
	public List<magicthronEntity> listAll();
	
	
	@Insert("INSERT INTO magicthron(idx,info) VALUES (0,0);")
	public void test();


}