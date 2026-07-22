<?php
class Admin_Model_DbTable_ParametersGroups extends Zend_Db_Table_Abstract
{
	protected $_name = 'parameters_groups';
	protected $_primary = 'id';
	
	/**
     * Returns array of groups
     * @return	array
     */
	public function getGroups()
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
     * Returns pairs of groups
     * @return	array
     */
	public function getGroupsPairs()
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
     * Returns primary keys
     * @return	int
     */
	public function setGroup($title)
	{
		$data = array('title'	=>	$title);
    	return $this->insert($data);    	
	}
	/**
     * Returns group
     * @param	int $id
     * @return	array
     */
	public function getGroup($id)
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
     * Update group
     * @param	int		$id
     * @param	string	$title
     * @return	void
     */
	public function updateGroup($id, $title)
	{
		$data = array('title'	=>	$title);
    	return $this->update($data, 'id = '.$id);
	}
	/**
     * set parameters_groups2parameters
     * @param	int	$group_id
     * @param	int	$parameter_id
     * @return	void
     */
	public function setGroups2parameters($group_id, $parameter_id)
	{
		$data = array(
			'parameters_groups_id'	=>	$group_id,
			'parameters_id'			=>	$parameter_id
		
		);
    	return $this->_db->insert('parameters_groups2parameters', $data);
	}
	/**
     * Returns array of parameters
     * @param	int	$group_id
     * @return	array
     */
	public function getParameters($group_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select
			->join(
				'parameters_groups2parameters',		  
				'parameters_groups2parameters.parameters_groups_id = parameters_groups.id',
				array('parameters_groups2parameters.id AS pg2p_id')
			)
			->join(
				'parameters',		  
				'parameters.id = parameters_groups2parameters.parameters_id',
				array('id', 'title')
			)
			->where('parameters_groups.id = ?', $group_id);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * del parameters_groups2parameters
     * @param	int	$pg2p_id
     * @return	void
     */
	public function delGroups2parameters($pg2p_id)
	{
		$this->_db->delete('parameters_groups2parameters', 'id = '.$pg2p_id);
	}
	/**
     * del group
     * @param	int	$id
     * @return	void
     */
	public function delGroup($id)
	{
		$this->_db->delete('parameters_groups2parameters', 'parameters_groups_id = '.$id);
		$this->delete('id = '.$id);
	}
}