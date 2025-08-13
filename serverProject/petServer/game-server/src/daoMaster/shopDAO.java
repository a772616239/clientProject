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
import model.shop.entity.shopEntity;

/**
 * created by tool
 */

public interface shopDAO extends BaseDAO<shopEntity> {

	@Insert("INSERT INTO shop(playeridx,shopinfo) VALUES (#{playeridx},#{shopinfo});")
	public void insert(shopEntity entity);

	@Delete("delete from shop where playeridx = #{playeridx}")
	public void delete(String idx);

	@Update("update shop set playeridx=#{playeridx},shopinfo=#{shopinfo} where playeridx = #{playeridx}")
	public void update(shopEntity entity);

	@Select("SELECT * FROM shop")
	public List<shopEntity> listAll();
	
	
	@Insert("INSERT INTO shop(playeridx,shopinfo) VALUES (0,0);")
	public void test();


}