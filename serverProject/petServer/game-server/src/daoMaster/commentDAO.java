/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.comment.entity.commentEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface commentDAO extends BaseDAO<commentEntity> {

	@Insert("INSERT INTO comment(idx,type,linkid,comment) VALUES (#{idx},#{type},#{linkid},#{comment});")
	public void insert(commentEntity entity);

	@Delete("delete from comment where idx = #{idx}")
	public void delete(String idx);

	@Update("update comment set idx=#{idx},type=#{type},linkid=#{linkid},comment=#{comment} where idx = #{idx}")
	public void update(commentEntity entity);

	@Select("SELECT * FROM comment")
	public List<commentEntity> listAll();
	
	
	@Insert("INSERT INTO comment(idx,type,linkid,comment) VALUES (0,0,0,0);")
	public void test();
}