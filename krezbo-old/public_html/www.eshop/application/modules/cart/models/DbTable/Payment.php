<?php
class Cart_Model_DbTable_Payment extends Zend_Db_Table_Abstract
{
	protected $_name = 'payment';
	protected $_primary = 'id';
	
	/**
     * Returns array of payment
     * @return	array
     */
	public function getPayment()
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
	/**
     * Returns payment
     * @param int	$id
     * @return	array
     */
	public function getPaymentByID($id)
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
     * Returns pairs of payment
     * @return	array
     */
	public function getPaymentPairs()
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