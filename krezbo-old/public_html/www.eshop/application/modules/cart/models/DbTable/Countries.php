<?php
class Cart_Model_DbTable_Countries extends Zend_Db_Table_Abstract
{
	protected $_name = 'countries';
	protected $_primary = 'id';
	
	/**
     * Returns array of countries
     * @return	array
     */
	public function getCountries()
	{
		$select = $this->select();
		$select->order('id ASC')
		       ->where('"show" IS NOT NULL');
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	
}