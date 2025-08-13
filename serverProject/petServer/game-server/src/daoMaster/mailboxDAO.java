/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.mailbox.entity.mailboxEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface mailboxDAO extends BaseDAO<mailboxEntity> {

	@Insert("INSERT INTO mailbox(idx,linkplayeridx,mailbox) VALUES (#{idx},#{linkplayeridx},#{mailbox});")
	public void insert(mailboxEntity entity);

	@Delete("delete from mailbox where idx = #{idx}")
	public void delete(String idx);

	@Update("update mailbox set idx=#{idx},linkplayeridx=#{linkplayeridx},mailbox=#{mailbox} where idx = #{idx}")
	public void update(mailboxEntity entity);

	@Select("SELECT * FROM mailbox")
	public List<mailboxEntity> listAll();
	
	
	@Insert("INSERT INTO mailbox(idx,linkplayeridx,mailbox) VALUES (0,0,0);")
	public void test();


}