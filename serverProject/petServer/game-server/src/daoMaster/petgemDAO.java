/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.petgem.entity.petgemEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface petgemDAO extends BaseDAO<petgemEntity> {

	@Insert("INSERT INTO petgem(idx,playeridx,gem,bagenlarge,capacity) VALUES (#{idx},#{playeridx},#{gem},#{bagenlarge},#{capacity});")
	public void insert(petgemEntity entity);

	@Delete("delete from petgem where idx = #{idx}")
	public void delete(String idx);

	@Update("update petgem set idx=#{idx},playeridx=#{playeridx},gem=#{gem},bagenlarge=#{bagenlarge},capacity=#{capacity} where idx = #{idx}")
	public void update(petgemEntity entity);

	@Select("SELECT * FROM petgem")
	public List<petgemEntity> listAll();
	
	
	@Insert("INSERT INTO petgem(idx,playeridx,gem,bagenlarge,capacity) VALUES (0,0,0,0,0);")
	public void test();


}