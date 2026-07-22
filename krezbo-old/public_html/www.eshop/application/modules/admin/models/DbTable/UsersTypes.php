<?php
class Admin_Model_DbTable_UsersTypes extends Zend_Db_Table_Abstract
{
	protected $_name = 'users_types';
	protected $_primary = 'id';
	/**
     * Return pairs of types
     * @return	array
     */
	public function getTypesPairs()
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