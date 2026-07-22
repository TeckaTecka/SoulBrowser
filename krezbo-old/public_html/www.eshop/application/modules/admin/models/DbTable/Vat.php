<?php
class Admin_Model_DbTable_Vat extends Zend_Db_Table_Abstract
{
	protected $_name = 'vat';
	protected $_primary = 'id';
	
	/**
     * Return array of Vats
     * @return	array
     */
	public function getVats()
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->order('vat ASC');
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return pairs of VAT
     * @return	array
     */
	public function getVatsPairs()
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->order('vat ASC');
		$rows = $this->_db->fetchPairs($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			//$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Set vat
     * @param	int	$vat
     * @return	int
     */
	public function setVat($vat)
	{
		$data = array(
			'title'	=>	$vat.'%',
			'vat'	=>	$vat
		);
    	return $this->insert($data);    	
	}
	/**
     * Return array of vat
     * @return	array
     */
	public function getVat($id)
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
     * Update vat
     * @param	int		$id
     * @param	int		$vat
     * @return	void
     */
	public function updateVat($id, $vat)
	{
		$data = array(
			'title'	=>	$vat.'%',
			'vat'	=>	$vat
		);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * set flag
	 * @param	int		$id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$id);
    }
}