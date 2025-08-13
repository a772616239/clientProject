/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.team.entity.teamEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface teamDAO extends BaseDAO<teamEntity> {

	@Insert("INSERT INTO team(idx,linkplayeridx,teamsinfo) VALUES (#{idx},#{linkplayeridx},#{teamsinfo});")
	public void insert(teamEntity entity);

	@Delete("delete from team where idx = #{idx}")
	public void delete(String idx);

	@Update("update team set idx=#{idx},linkplayeridx=#{linkplayeridx},teamsinfo=#{teamsinfo} where idx = #{idx}")
	public void update(teamEntity entity);

	@Select("SELECT * FROM team")
	public List<teamEntity> listAll();

	@Insert("INSERT INTO team(idx,linkplayeridx,teamsinfo) VALUES (0,0,0);")
	public void test();


}