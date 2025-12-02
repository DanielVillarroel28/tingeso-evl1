import { useEffect, useState } from "react";
import {
    Box,
    TextField,
    Button,
    Paper,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    MenuItem
} from "@mui/material";
import kardexService from "../services/kardex.service";
import toolService from "../services/tool.service";

const KardexView = () => {
    const [movements, setMovements] = useState([]);
    const [uniqueToolNames, setUniqueToolNames] = useState([]); // Guardará los nombres únicos
    const [selectedToolName, setSelectedToolName] = useState(''); // Estado para el filtro por nombre
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const init = () => {
        kardexService.getMovements().then(response => setMovements(response.data));
        // Cargar todas las herramientas para poblar el dropdown de filtros
        toolService.getAll().then(response => {
            // Extraer nombres únicos para el dropdown, sin importar mayúsculas/minúsculas
            const names = [...new Set(response.data.map(tool => tool.name.toLowerCase()))];
            setUniqueToolNames(names.sort()); // Ordenar alfabéticamente
        });
    };

    useEffect(() => { init(); }, []);

    const handleFilter = () => {
        const params = {};
        if (selectedToolName) params.toolName = selectedToolName;
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;

        kardexService.getMovements(params)
            .then(response => setMovements(response.data))
            .catch(error => console.error("Error al filtrar movimientos.", error));
    };

    const handleClearFilters = () => {
        setSelectedToolName('');
        setStartDate('');
        setEndDate('');
        kardexService.getMovements().then(response => setMovements(response.data));
    };
    
    const formatDateTime = (dateTimeString) => {
        if (!dateTimeString) return 'N/A';
        return new Date(dateTimeString).toLocaleString('es-CL');
    };

    return (
        <Box sx={{ margin: 2 }}>
            <Typography variant="h4" gutterBottom>
                Historial de Movimientos (Kardex)
            </Typography>

            <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
                <TextField
                    select
                    label="Herramienta"
                    value={selectedToolName}
                    onChange={(e) => setSelectedToolName(e.target.value)}
                    variant="outlined"
                    size="small"
                    sx={{ minWidth: 200 }}
                >
                    <MenuItem value=""><em>Todas</em></MenuItem>
                    {uniqueToolNames.map(name => (
                        <MenuItem key={name} value={name}>
                            {/* Capitalizar para mostrarlo bonito en el menú */}
                            {name.charAt(0).toUpperCase() + name.slice(1)}
                        </MenuItem>
                    ))}
                </TextField>
                <TextField label="Fecha Desde" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} InputLabelProps={{ shrink: true }} size="small" />
                <TextField label="Fecha Hasta" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} InputLabelProps={{ shrink: true }} size="small" />
                <Button variant="contained" onClick={handleFilter}>Filtrar</Button>
                <Button variant="outlined" onClick={handleClearFilters}>Limpiar</Button>
            </Paper>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ fontWeight: 'bold' }}>Fecha</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Herramienta</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Tipo de Movimiento</TableCell>
                            <TableCell align="right" sx={{ fontWeight: 'bold' }}>Cantidad Afectada</TableCell>
                            <TableCell sx={{ fontWeight: 'bold' }}>Responsable</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {movements.map((movement) => (
                            <TableRow key={movement.id}>
                                <TableCell>{formatDateTime(movement.movementDate)}</TableCell>
                                <TableCell>{movement.tool?.name} (ID: {movement.tool?.id})</TableCell>
                                <TableCell>{movement.movementType}</TableCell>
                                <TableCell align="right">{movement.quantityAffected}</TableCell>
                                <TableCell>{movement.userResponsible}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
};

export default KardexView;