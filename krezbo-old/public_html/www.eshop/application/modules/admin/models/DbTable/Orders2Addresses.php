<?php
class Admin_Model_DbTable_Orders2Addresses extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders2addresses';
	protected $_primary = 'id';
	/**
     * Return array of Addresses of order
     * @param int	$order_id
     * @return	array
     */
	/*public function getAddresses($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('addresses',		  
		       		  'addresses.id = orders2addresses.addresses_id', array(''))
			   ->where('orders2products.orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}*/
	
}