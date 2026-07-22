<?php
class Admin_Model_DbTable_OrdersStatuses extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders_statuses';
	protected $_primary = 'id';
	/**
     * Return pairs of statuses
     * @return	array
     */
	public function getStatusesPairs()
	{
		$select = $this->select();
		$rows = $this->_db->fetchPairs($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			return $rows;
		}
	}
}