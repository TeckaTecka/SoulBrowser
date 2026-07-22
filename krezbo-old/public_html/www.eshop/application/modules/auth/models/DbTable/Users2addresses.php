<?php
class Auth_Model_DbTable_Users2addresses extends Zend_Db_Table_Abstract
{
	protected $_name = 'users2addresses';
	protected $_primary = 'id';
	
	/**
     * Add row to DB
     * @param	int		$users_id
     * @param	int		$address_id
     * @return	primary key
     */
	public function setUsers2addresses($users_id, $address_id)
	{
		$data = array(
    		'users_id'		=>	$users_id,
    		'addresses_id'	=>	$address_id
    	);
        return $this->insert($data);
	}
}