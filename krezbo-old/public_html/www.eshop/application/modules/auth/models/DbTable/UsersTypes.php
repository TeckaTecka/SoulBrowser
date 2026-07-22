<?php
class Auth_Model_DbTable_UsersTypes extends Zend_Db_Table_Abstract
{
	protected $_name = 'users_types';
	protected $_primary = 'id';
	
	/**
     * Returns array of users_types
     * @return	array
     */
	public function getTypes()
	{
		$select = $this->select();
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	
}