<?php
class Cart_Model_DbTable_Orders2Users extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders2users';
	protected $_primary = 'id';
	
	/**
     * Add row to DB
     * @param	int		$users_id
     * @param	int		$addresses_id
     * @return	primary key
     */
	public function setOrders2Users($users_id, $orders_id)
    {
    	$data = array(
    		'users_id'		=>	$users_id,
    		'orders_id'		=>	$orders_id
    	);
        return $this->insert($data);
    }
}