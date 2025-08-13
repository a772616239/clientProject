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
import model.petmission.entity.petmissionEntity;

/**
 * created by tool
 */

public interface petmissionDAO extends BaseDAO<petmissionEntity> {

	@Insert("INSERT INTO petmission(idx,playeridx,mission,acceptedmission) VALUES (#{idx},#{playeridx},#{mission},#{acceptedmission});")
	public void insert(petmissionEntity entity);

	@Delete("delete from petmission where idx = #{idx}")
	public void delete(String idx);

	@Update("update petmission set idx=#{idx},playeridx=#{playeridx},mission=#{mission},acceptedmission=#{acceptedmission} where idx = #{idx}")
	public void update(petmissionEntity entity);

	@Select("SELECT * FROM petmission")
	public List<petmissionEntity> listAll();
	
	
	@Insert("INSERT INTO petmission(idx,playeridx,mission,acceptedmission) VALUES (0,0,0,0);")
	public void test();


}