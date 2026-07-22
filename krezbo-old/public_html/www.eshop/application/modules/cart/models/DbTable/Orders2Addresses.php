<?php
class Cart_Model_DbTable_Orders2Addresses extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders2addresses';
	protected $_primary = 'id';
	
	/**
     * Add row to DB
     * @param	int		$orders_id
     * @param	int		$addresses_id
     * @param	boolean	$billing
     * @param	boolean	$delivery
     * @return	primary key
     */
	public function setOrders2Address($orders_id, $addresses_id, $billing, $delivery)
    {
    	$data = array(
    		'orders_id'		=>	$orders_id,
    		'addresses_id'	=>	$addresses_id,
    		'billing'		=>	$billing,
    		'delivery'		=>	$delivery
    	);
        return $this->insert($data);
    }
	
}