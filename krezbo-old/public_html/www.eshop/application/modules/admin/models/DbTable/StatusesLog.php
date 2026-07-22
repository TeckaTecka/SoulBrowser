<?php
class Admin_Model_DbTable_StatusesLog extends Zend_Db_Table_Abstract
{
	protected $_name = 'statuses_log';
	protected $_primary = 'id';
	/**
     * Return array of status lods
     * @param	int	$order_id
     * @return	array
     */
	public function getStatusesLogsByOrderID($order_id)
	{
		$select = $this->select();
		$select->where('orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Set status log
     * @param	int	$order_id
     * @param	int	$original_status_id
     * @param	int	$new_status_id
     * @return	void
     */
	public function setLog($order_id, $original_status_id, $new_status_id)
	{
		$data = array(
			'orders_id'	=>	$order_id,
			'original'	=>	$original_status_id,
			'new'		=>	$new_status_id);
    	return $this->insert($data);    	
	}
	
}