/**
 * created by tool DAOGenerate
 */
package daoMaster;


import db.core.BaseDAO;
import model.pet.entity.petEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by tool
 */

public interface petDAO extends BaseDAO<petEntity> {

	@Insert("INSERT INTO pet(idx,playeridx,pet,bagenlarge,capacity,occupancy,collection) VALUES (#{idx},#{playeridx},#{pet},#{bagenlarge},#{capacity},#{occupancy},#{collection});")
	public void insert(petEntity entity);

	@Delete("delete from pet where idx = #{idx}")
	public void delete(String idx);

	@Update("update pet set idx=#{idx},playeridx=#{playeridx},pet=#{pet},bagenlarge=#{bagenlarge},capacity=#{capacity},occupancy=#{occupancy},collection=#{collection} where idx = #{idx}")
	public void update(petEntity entity);

	@Select("SELECT * FROM pet")
	public List<petEntity> listAll();
	
	
	@Insert("INSERT INTO pet(idx,playeridx,pet,bagenlarge,capacity,occupancy,collection) VALUES (0,0,0,0,0,0,0);")
	public void test();
}