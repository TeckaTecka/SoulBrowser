<?php
class Admin_Model_DbTable_Consumptions extends Zend_Db_Table_Abstract
{
	protected $_name = 'consumption';
	protected $_primary = 'id';
	/**
     * Return array of Consumptions
     * @return	array
     */
	public function getConsumptions()
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
     * Return pairs of Consumptions
     * @return	array
     */
	public function getConsumptionsPairs()
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
     * Set Consumption
     * @param	string	$Consumption
     * @param	boolean	$show
     * @return	int
     */
	public function setConsuption($consumption, $price, $show)
	{
		$data = array(
			'consumption'	=>	$consumption,
			'price'			=>	($price=='')?NULL:$price,
			'show'			=>	$show);
    	return $this->insert($data);    	
	}
	/**
     * Return array of Consumption
     * @return	array
     */
	public function getConsumption($id)
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
     * Update Consumption
     * @param	int		$id
     * @param	string	$consumption
     * @param	int		$show
     * @return	void
     */
	public function updateConsumption($id, $consumption, $price, $show)
	{
		$data = array(
			'consumption'	=>	$consumption,
			'price'			=>	($price=='')?NULL:$price,
			'show'			=>	$show
		);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete Consumptions
     * @param	int	$id
     * @return	void
     */
	public function delConsumption($id)
    {
    	$this->delete('id = '.$id);
    }
}