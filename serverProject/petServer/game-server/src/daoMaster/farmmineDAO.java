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
import model.farmmine.entity.farmmineEntity;

/**
 * created by tool
 */

public interface farmmineDAO extends BaseDAO<farmmineEntity> {

	@Insert("INSERT INTO farmmine(idx,baseidx,extids,petid,titleid,occplayerid,price,jointime,auctionend,auctionstart,auctioninfo,playerdata,zerodata) VALUES (#{idx},#{baseidx},#{extids},#{petid},#{titleid},#{occplayerid},#{price},#{jointime},#{auctionend},#{auctionstart},#{auctioninfo},#{playerdata},#{zerodata});")
	public void insert(farmmineEntity entity);

	@Delete("delete from farmmine where idx = #{idx}")
	public void delete(String idx);

	@Update("update farmmine set idx=#{idx},baseidx=#{baseidx},extids=#{extids},petid=#{petid},titleid=#{titleid},occplayerid=#{occplayerid},price=#{price},jointime=#{jointime},auctionend=#{auctionend},auctionstart=#{auctionstart},auctioninfo=#{auctioninfo},playerdata=#{playerdata},zerodata=#{zerodata} where idx = #{idx}")
	public void update(farmmineEntity entity);

	@Select("SELECT * FROM farmmine")
	public List<farmmineEntity> listAll();
	
	
	@Insert("INSERT INTO farmmine(idx,baseidx,extids,petid,titleid,occplayerid,price,jointime,auctionend,auctionstart,auctioninfo,playerdata,zerodata) VALUES (0,0,0,0,0,0,0,0,0,0,0,0,0);")
	public void test();


}