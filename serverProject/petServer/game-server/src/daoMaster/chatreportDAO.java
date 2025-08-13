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
import model.chatreport.entity.chatreportEntity;

/**
 * created by tool
 */

public interface chatreportDAO extends BaseDAO<chatreportEntity> {

	@Insert("INSERT INTO chatreport(idx,content,linkplayer,info) VALUES (#{idx},#{content},#{linkplayer},#{info});")
	public void insert(chatreportEntity entity);

	@Delete("delete from chatreport where idx = #{idx}")
	public void delete(String idx);

	@Update("update chatreport set idx=#{idx},content=#{content},linkplayer=#{linkplayer},info=#{info} where idx = #{idx}")
	public void update(chatreportEntity entity);

	@Select("SELECT * FROM chatreport")
	public List<chatreportEntity> listAll();


	@Insert("INSERT INTO chatreport(idx,content,linkplayer,info) VALUES (0,0,0,0);")
	public void test();


}