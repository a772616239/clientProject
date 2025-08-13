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
import model.training.entity.trainingEntity;

/**
 * created by tool
 */

public interface trainingDAO extends BaseDAO<trainingEntity> {

	@Insert("INSERT INTO training(idx,playeridx,opens,infos,finishinfo) VALUES (#{idx},#{playeridx},#{opens},#{infos},#{finishinfo});")
	public void insert(trainingEntity entity);

	@Delete("delete from training where idx = #{idx}")
	public void delete(String idx);

	@Update("update training set idx=#{idx},playeridx=#{playeridx},opens=#{opens},infos=#{infos},finishinfo=#{finishinfo} where idx = #{idx}")
	public void update(trainingEntity entity);

	@Select("SELECT * FROM training")
	public List<trainingEntity> listAll();
	
	
	@Insert("INSERT INTO training(idx,playeridx,opens,infos,finishinfo) VALUES (0,0,0,0,0);")
	public void test();


}