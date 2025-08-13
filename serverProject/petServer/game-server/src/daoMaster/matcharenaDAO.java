/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.matcharena.entity.matcharenaEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface matcharenaDAO extends BaseDAO<matcharenaEntity> {

	@Insert("INSERT INTO matcharena(idx,data) VALUES (#{idx},#{data});")
	public void insert(matcharenaEntity entity);

	@Delete("delete from matcharena where idx = #{idx}")
	public void delete(String idx);

	@Update("update matcharena set idx=#{idx},data=#{data} where idx = #{idx}")
	public void update(matcharenaEntity entity);

	@Select("SELECT * FROM matcharena")
	public List<matcharenaEntity> listAll();
	
	
	@Insert("INSERT INTO matcharena(idx,data) VALUES (0,0);")
	public void test();


}