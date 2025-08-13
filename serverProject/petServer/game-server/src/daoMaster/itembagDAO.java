/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.itembag.entity.itembagEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


/**
 * created by tool
 */

public interface itembagDAO extends BaseDAO<itembagEntity> {

	@Insert("INSERT INTO itembag(idx,linkplayeridx,iteminfo) VALUES (#{idx},#{linkplayeridx},#{iteminfo});")
	public void insert(itembagEntity entity);

	@Delete("delete from itembag where idx = #{idx}")
	public void delete(String idx);

	@Update("update itembag set idx=#{idx},linkplayeridx=#{linkplayeridx},iteminfo=#{iteminfo} where idx = #{idx}")
	public void update(itembagEntity entity);

	@Select("SELECT * FROM itembag")
	public List<itembagEntity> listAll();
	
	
	@Insert("INSERT INTO itembag(idx,linkplayeridx,iteminfo) VALUES (0,0,0);")
	public void test();


}