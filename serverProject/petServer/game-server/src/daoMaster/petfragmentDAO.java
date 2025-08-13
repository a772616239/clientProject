/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.petfragment.entity.petfragmentEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface petfragmentDAO extends BaseDAO<petfragmentEntity> {

	@Insert("INSERT INTO petfragment(idx,playeridx,fragment) VALUES (#{idx},#{playeridx},#{fragment});")
	public void insert(petfragmentEntity entity);

	@Delete("delete from petfragment where idx = #{idx}")
	public void delete(String idx);

	@Update("update petfragment set idx=#{idx},playeridx=#{playeridx},fragment=#{fragment} where idx = #{idx}")
	public void update(petfragmentEntity entity);

	@Select("SELECT * FROM petfragment")
	public List<petfragmentEntity> listAll();
	
	
	@Insert("INSERT INTO petfragment(idx,playeridx,fragment) VALUES (0,0,0);")
	public void test();


}