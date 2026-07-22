<?php
class Cart_Model_DbTable_Consumption extends Zend_Db_Table_Abstract
{
	protected $_name = 'consumption';
	protected $_primary = 'id';
	
	/**
     * Returns array of consumption
     * @return	array
     */
	public function getConsumption()
	{
		$select = $this->select();
		/*$select->join('categories_bd',		  
		       		  'categories_bd.categories_id=categories.id', array('sub', 'order'))
			   ->order(array('categories_bd.order ASC'))
		       ->where('categories_bd.sub = ?', $sub);*/
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Returns consumption
     * @param int $id
     * @return	array
     */
	public function getConsumptionByID($id)
	{
		$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Returns pairs of Consumptions
     * @return	array
     */
	public function getConsumptionsPairs()
	{
		$select = $this->select();
		$rows = $this->_db->fetchPairs($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			//$rows = $rows->toArray();
			return $rows;
		}
	}
}