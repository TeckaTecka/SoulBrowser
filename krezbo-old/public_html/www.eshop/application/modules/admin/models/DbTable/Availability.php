<?php
class Admin_Model_DbTable_Availability extends Zend_Db_Table_Abstract
{
	protected $_name = 'availability';
	protected $_primary = 'id';
	
	/**
     * Return array of availability
     * @return	array
     */
	public function getAvailabilityAll()
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->order('title ASC');
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return pairs of availability
     * @return	array
     */
	public function getAvailabilityAllPairs()
	{
		$select = $this->select();
		$select
			//->where('flags IS NULL')
			->order('title ASC');
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
     * @param	int	$availability
     * @return	int
     */
	public function setAvailability($availability)
	{
		$data = array(
			'title'	=>	$availability
		);
    	return $this->insert($data);    	
	}
	/**
     * Return array of availability
     * @return	array
     */
	public function getAvailability($id)
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
     * Update availability
     * @param	int		$id
     * @param	int		$availability
     * @return	void
     */
	public function updateAvailability($id, $availability)
	{
		$data = array(
			'title'	=>	$availability
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