<?php
class Admin_Model_DbTable_Payments extends Zend_Db_Table_Abstract
{
	protected $_name = 'payment';
	protected $_primary = 'id';
	/**
     * Return array of payments
     * @return	array
     */
	public function getPayments()
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
     * Return pairs of payments
     * @return	array
     */
	public function getPaymentsPairs()
	{
		$select = $this->select();
		$rows = $this->_db->fetchPairs($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			return $rows;
		}
	}
	/**
     * Set Payment
     * @param	string	$country
     * @param	boolean	$show
     * @return	int
     */
	public function setPayment($payment, $price, $show)
	{
		$data = array(
			'payment'	=>	$payment,
			'price'		=>	($price=='')?NULL:$price,
			'show'		=>	$show);
    	return $this->insert($data);    	
	}
	/**
     * Return array of payment
     * @return	array
     */
	public function getPayment($id)
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
     * Update payment
     * @param	int		$id
     * @param	string	$payment
     * @param	int		$show
     * @return	void
     */
	public function updatePayment($id, $payment, $price, $show)
	{
		$data = array(
			'payment'	=>	$payment,
			'price'		=>	($price=='')?NULL:$price,
			'show'		=>	$show
		);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete payment
     * @param	int	$id
     * @return	void
     */
	public function delPayment($id)
    {
    	$this->delete('id = '.$id);
    }
}