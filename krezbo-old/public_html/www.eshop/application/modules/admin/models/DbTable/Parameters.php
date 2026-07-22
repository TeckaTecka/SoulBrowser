<?php
class Admin_Model_DbTable_Parameters extends Zend_Db_Table_Abstract
{
	protected $_name = 'parameters';
	protected $_primary = 'id';
	/**
     * Return array of parameters
     * @return	array
     */
	public function getParameters($page)
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->limitPage($page, 20)
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
     * Return Pairs of parameters
     * @return	array
     */
	public function getParametersPairs()
	{
		$select = $this->select();
		$select
			->where('flags IS NULL')
			->order('title ASC');
		$rows = $this->_db->fetchPairs($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			return $rows;
		}
	}
	/**
     * Set parameter
     * @param	string	$parameter
     * @return	int
     */
	public function setParameter($parameter)
	{
		$data = array('title'	=>	$parameter);
    	return $this->insert($data);    	
	}
	/**
     * Return array of parameter
     * @return	array
     */
	public function getParameter($id)
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
     * Update parameter
     * @param	int		$id
     * @param	string	$title
     * @return	void
     */
	public function updateParameter($id, $title)
	{
		$data = array(
			'title'	=>	$title
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