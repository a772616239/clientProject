/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import java.util.List;
import model.inscription.petinscriptionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * created by tool
 */

public interface petinscriptionDAO extends BaseDAO<petinscriptionEntity> {

	@Insert("INSERT INTO petinscription(idx,playeridx,inscription) VALUES (#{idx},#{playeridx},#{inscription});")
	public void insert(petinscriptionEntity entity);

	@Delete("delete from petinscription where idx = #{idx}")
	public void delete(String idx);

	@Update("update petinscription set idx=#{idx},playeridx=#{playeridx},inscription=#{inscription} where idx = #{idx}")
	public void update(petinscriptionEntity entity);

	@Select("SELECT * FROM petinscription")
	public List<petinscriptionEntity> listAll();
	
	
	@Insert("INSERT INTO petinscription(idx,playeridx,inscription) VALUES (0,0,0);")
	public void test();


}