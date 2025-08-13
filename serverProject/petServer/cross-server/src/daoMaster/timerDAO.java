/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import java.util.List;
import model.timer.entity.timerEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * created by tool
 */

public interface timerDAO extends BaseDAO<timerEntity> {

	@Insert("INSERT INTO timer(idx,starttime,expiretype,cycleinterval,nexttriggertime,expireparam,alreadytriggertimes,targettype,params) VALUES (#{idx},#{starttime},#{expiretype},#{cycleinterval},#{nexttriggertime},#{expireparam},#{alreadytriggertimes},#{targettype},#{params});")
	public void insert(timerEntity entity);

	@Delete("delete from timer where idx = #{idx}")
	public void delete(String idx);

	@Update("update timer set idx=#{idx},starttime=#{starttime},expiretype=#{expiretype},cycleinterval=#{cycleinterval},nexttriggertime=#{nexttriggertime},expireparam=#{expireparam},alreadytriggertimes=#{alreadytriggertimes},targettype=#{targettype},params=#{params} where idx = #{idx}")
	public void update(timerEntity entity);

	@Select("SELECT * FROM timer")
	public List<timerEntity> listAll();
	
	
	@Insert("INSERT INTO timer(idx,starttime,expiretype,cycleinterval,nexttriggertime,expireparam,alreadytriggertimes,targettype,params) VALUES (0,0,0,0,0,0,0,0,0);")
	public void test();
}